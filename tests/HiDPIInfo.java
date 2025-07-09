/*
 * Copyright 2025 JetBrains s.r.o.
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
 * @summary Verifies that HiDPIInfo.getInfo() works on Linux
 * @requires os.family == "linux"
 * @run main/othervm HiDPIInfo
 */
import com.jetbrains.JBR;

public class HiDPIInfo {

    public static void main(String[] args) throws Exception {
        String[][] info = JBR.getHiDPIInfo().getInfo(); 
        for (var row:info) {
            for (String s: row) {
                System.out.print(s);
                System.out.print("\t");
            }
            System.out.println();
        }
    }
}
