/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.server.wm.flicker.monitor;

import static com.android.server.wm.nano.WindowManagerTraceFileProto.MAGIC_NUMBER_H;
import static com.android.server.wm.nano.WindowManagerTraceFileProto.MAGIC_NUMBER_L;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.runner.AndroidJUnit4;

import com.android.server.wm.nano.WindowManagerTraceFileProto;

import com.google.common.io.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.nio.file.Path;

/**
 * Contains {@link WindowManagerTraceMonitor} tests. To run this test: {@code atest
 * FlickerLibTest:WindowManagerTraceMonitorTest}
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WindowManagerTraceMonitorTest {
    private WindowManagerTraceMonitor mWindowManagerTraceMonitor;

    @Before
    public void setup() {
        mWindowManagerTraceMonitor = new WindowManagerTraceMonitor();
    }

    @After
    public void teardown() {
        mWindowManagerTraceMonitor.stop();
        mWindowManagerTraceMonitor.getOutputTraceFilePath("captureWindowTrace").toFile().delete();
    }

    @Test
    public void canStartWindowTrace() throws Exception {
        mWindowManagerTraceMonitor.start();
        assertThat(mWindowManagerTraceMonitor.isEnabled()).isTrue();
    }

    @Test
    public void canStopWindowTrace() throws Exception {
        mWindowManagerTraceMonitor.start();
        assertThat(mWindowManagerTraceMonitor.isEnabled()).isTrue();
        mWindowManagerTraceMonitor.stop();
        assertThat(mWindowManagerTraceMonitor.isEnabled()).isFalse();
    }

    @Test
    public void captureWindowTrace() throws Exception {
        mWindowManagerTraceMonitor.start();
        mWindowManagerTraceMonitor.stop();
        Path testFilePath = mWindowManagerTraceMonitor.save("captureWindowTrace");
        File testFile = testFilePath.toFile();
        assertThat(testFile.exists()).isTrue();
        String calculatedChecksum = TraceMonitor.calculateChecksum(testFilePath);
        assertThat(calculatedChecksum).isEqualTo(mWindowManagerTraceMonitor.getChecksum());
        byte[] trace = Files.toByteArray(testFile);
        assertThat(trace.length).isGreaterThan(0);
        WindowManagerTraceFileProto mWindowTraceFileProto =
                WindowManagerTraceFileProto.parseFrom(trace);
        assertThat(mWindowTraceFileProto.magicNumber)
                .isEqualTo((long) MAGIC_NUMBER_H << 32 | MAGIC_NUMBER_L);
    }
}
