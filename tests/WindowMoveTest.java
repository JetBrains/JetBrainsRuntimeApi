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

/*
 * @test
 * @summary Verifies that WindowMove test service of JBR is supported
 *          on Linux and not supported elsewhere.
 * @run main WindowMoveTest
 * @run main/othervm -Djava.awt.headless=true WindowMoveTest
 */

import com.jetbrains.JBR;
import java.awt.GraphicsEnvironment;

public class WindowMoveTest {

    public static void main(String[] args) throws Exception {
        final String os = System.getProperty("os.name");
        if ("linux".equalsIgnoreCase(os)) {
            final boolean isHeadless = GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadlessInstance();
            if (JBR.isWindowMoveSupported()) {
                if (isHeadless)
                    throw new RuntimeException("JBR.isWindowMoveSupported says it is supported in the headless mode");
            } else {
                if (!isHeadless)
                    throw new RuntimeException("JBR.isWindowMoveSupported says it is NOT supported on Linux");
            }
            // Use: JBR.getWindowMove().startMovingTogetherWithMouse(jframe, MouseEvent.BUTTON1);
        } else {
            if (JBR.isWindowMoveSupported()) {
                throw new RuntimeException("JBR.isWindowMoveSupported says it's supported on " + os + "where it is NOT implemented");
            }
        }
    }
}
