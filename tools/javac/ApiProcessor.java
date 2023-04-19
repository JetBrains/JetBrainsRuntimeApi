/*
 * Copyright 2000-2024 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings({"UnnecessaryUnicodeEscape", "unused"})
@SupportedOptions({"output", "version"})
@SupportedSourceVersion(SourceVersion.RELEASE_9)
@SupportedAnnotationTypes("*")
public class ApiProcessor extends AbstractProcessor {

    private Path output;
    private Api.Version versionOverride;
    private SourceGenerator sourceGenerator;
    private ApiCollector apiCollector;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        output = Path.of(Objects.requireNonNull(processingEnv.getOptions().get("output"), "-Aoutput option is missing"));
        String version = processingEnv.getOptions().get("version");
        if (version != null) versionOverride = Api.Version.parse(version);

        sourceGenerator = new SourceGenerator(processingEnv);
        apiCollector = new ApiCollector(processingEnv);
    }

    private static ExecutableElement findAnnotationValue(TypeElement e) {
        for (Element t : e.getEnclosedElements()) {
            if (t instanceof ExecutableElement executable) {
                if (t.getSimpleName().toString().equals("value")) {
                    return executable;
                }
            }
        }
        throw new Error(e.getQualifiedName() + ".value() method not found");
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        // Find our annotation elements.
        Round round = new Round(roundEnvironment);
        for (TypeElement e : set) {
            switch (e.getQualifiedName().toString()) {
                case "com.jetbrains.Service"   -> round.annotations.service  = e;
                case "com.jetbrains.Provided"  -> round.annotations.provided = e;
                case "com.jetbrains.Provides"  -> round.annotations.provides = e;
                case "com.jetbrains.Fallback"   -> {
                    round.annotations.fallback  = e;
                    round.annotations.fallbackValue = findAnnotationValue(e);
                }
                case "com.jetbrains.Extension" -> {
                    round.annotations.extension = e;
                    round.annotations.extensionValue = findAnnotationValue(e);
                }
            }
        }

        // Generate sources on first round.
        if (sourceGenerator != null) {
            sourceGenerator.generate(round);
            sourceGenerator = null;
        }

        // Collect API info.
        Api.Module newApi = apiCollector.collect(round);
        if (newApi == null) return true;
        try {
            String message;
            if (versionOverride != null) {
                // Override API version from options.
                newApi.version = versionOverride;
                message = "\u2757 Skipping API checks, version override specified: " + versionOverride + "\n";
            } else {
                // Read old API info.
                Api.Module oldApi;
                try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("api-blob"))) {
                    oldApi = (Api.Module) in.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                // Compare API changes.
                ApiComparator.Node result = ApiComparator.compare(oldApi, newApi);
                ApiComparator.Digest digest = result.digest();
                newApi.version = digest.compatibility().incrementVersion(oldApi.version);
                if (digest.compatibility() == ApiComparator.Compatibility.SAME) {
                    // Do not print anything if there were no changes.
                    message = null;
                } else {
                    // Put changes into code block.
                    StringBuilder out = new StringBuilder();
                    if (!digest.diff().isEmpty()) out.append("```\n").append(digest.diff()).append("```\n");
                    // Print messages.
                    for (ApiComparator.Message msg : digest.messages()) out.append(msg.text).append('\n');
                    out.append("Compatibility status of API changes: ").append(digest.compatibility()).append(' ');
                    out.append(switch (digest.compatibility()) {
                        case MAJOR -> "\uD83E\uDD2F";
                        case MINOR -> "\uD83D\uDD27";
                        case PATCH -> "\uD83D\uDC85";
                        default    -> "";
                    });
                    out.append("\nVersion increment: ").append(oldApi.version).append(" -> ").append(newApi.version).append('\n');
                    message = out.toString();
                }
            }
            if (message != null) {
                // Get rid of unicode symbols when printing to stdout.
                String simple = message;
                for (ApiComparator.Message msg : ApiComparator.Message.values()) {
                    if (msg.mark != null && msg.simpleMark != null) simple = simple.replaceAll(msg.mark, msg.simpleMark);
                }
                simple = simple
                        .replaceAll("[^\\x00-\\x7F]", "")
                        .replaceAll("```\n", "")
                        .stripTrailing();
                System.out.println(simple);
            }

            // Save metadata.
            Files.createDirectories(output);
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(output.resolve("api-blob")))) {
                out.writeObject(newApi);
            }
            Files.writeString(output.resolve("version.txt"), newApi.version.toString());
            Files.writeString(output.resolve("message.txt"), message != null ? message : "");
            Files.write(output.resolve("sourcelist8.txt"), apiCollector.getJava8CompilationUnitPaths());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
