/*
 * Copyright 2024 JetBrains s.r.o.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.jetbrains;

/**
 * Allows to disable the equality check in CCursorManager to work around a macOS bug
 * <p>
 *     When the cursor is set using {@code Component.setCursor}, on macOS it eventually gets
 *     to {@code CCursorManager.setCursor} which then calls the native method responsible
 *     for mouse cursor changing on macOS. However, sometimes it doesn't do anything when called.
 *     And when the same method is called again, an equality check is performed,
 *     and if the cursor is the same, the native method isn't called.
 * </p>
 * <p>
 *     This API can be used to disable that equality check, which solves the issue of changing the cursor,
 *     as the native method works on most tries correctly. It can be used as a workaround in places
 *     where having the correct cursor is critical, for example, in resize operations where the user
 *     would otherwise have no visual indication of whether resize is currently possible.
 * </p>
 */
@Service
@Provided
public interface CursorEqualityCheck {
    /**
     * Enables or disables the cursor equality check on macOS
     * <p>
     *     Disabling the equality check may have a performance impact,
     *     so it should only be done temporarily when really needed.
     * </p>
     * @param enabled the new equality check state
     */
    void setEnabled(boolean enabled);
}
