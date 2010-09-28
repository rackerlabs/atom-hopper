/*
 *  Copyright 2010 zinic.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package com.rackspace.cloud.sense.util;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
/**
 *
 * @author zinic
 */
@RunWith(Enclosed.class)
public class UtilitiesTest {
    public static class WhenCheckingIfAStringIsBlank {
        @Test
        public void shouldHandleNulls() {
            assertTrue(Utilities.stringIsBlank(null));
        }

        @Test
        public void shouldHandleEmptyStrings() {
            assertTrue(Utilities.stringIsBlank(null));
        }

        @Test
        public void shouldHandleBlankStrings() {
            assertTrue(Utilities.stringIsBlank("     "));
        }

        @Test
        public void shouldHandleBlankStringsWithNewLines() {
            assertTrue(Utilities.stringIsBlank("\n\n"));
        }

        @Test
        public void shouldHandleBlankStringsWithTabs() {
            assertTrue(Utilities.stringIsBlank("\t\t"));
        }

        @Test
        public void shouldHandleComplexBlankStrings() {
            assertTrue(Utilities.stringIsBlank("\n\n  \t  \t\n  \t\n   \n\t"));
        }

        @Test
        public void shouldRejectComplexNonBlankStrings() {
            assertFalse(Utilities.stringIsBlank("\n\n  \t abc123 \t\n  \t\n   \n\t"));
        }

        @Test
        public void shouldRejectNonBlankStrings() {
            assertFalse(Utilities.stringIsBlank("zf-adapter"));
        }
    }
}
