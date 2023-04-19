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

package com.jetbrains;

import javax.accessibility.Accessible;

/**
 * This interface provides the ability to speak a given string using screen readers.
 *
 */
@Service
@Provided
public interface AccessibleAnnouncer {

    /**
     * Messages do not interrupt the current speech, they are spoken after the screen reader has spoken the current phrase
     */
    public static final int ANNOUNCE_WITHOUT_INTERRUPTING_CURRENT_OUTPUT = 0;

    /**
     * Messages interrupt the current speech, but only when the focus is on the window of the calling application
     */
    public static final int ANNOUNCE_WITH_INTERRUPTING_CURRENT_OUTPUT = 1;

    /**
     * This method makes an announcement with the specified priority from an accessible to which the announcing relates
     *
     * @param a        an accessible to which the announcing relates
     * @param str      string for announcing
     * @param priority priority for announcing
     */
    void announce(Accessible a, final String str, final int priority);
}
