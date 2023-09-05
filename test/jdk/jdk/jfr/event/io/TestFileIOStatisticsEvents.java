/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
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

package jdk.jfr.event.io;

import static jdk.test.lib.Asserts.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordedEvent;
import jdk.test.lib.jfr.EventNames;
import jdk.test.lib.jfr.Events;
import jdk.test.lib.Utils;
import static jdk.test.lib.Asserts.assertGreaterThanOrEqual;

/**
 * @test
 * @key jfr
 * @requires vm.hasJFR
 * @library /test/lib /test/jdk
 * @run main/othervm jdk.jfr.event.io.TestFileIOStatisticsEvents
 */
public class TestFileIOStatisticsEvents {
    private static final int writeInt = 'A';
    private static final byte[] writeBuf = { 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T' };
    private static int accWriteExpected = 0;
    private static int accReadExpected = 0;

    public static void main(String[] args) throws Throwable {
        Recording recording = new Recording();
        File tmp = Utils.createTempFile("TestFileIOStatistics", ".tmp").toFile();
        recording.enable(EventNames.FileReadIOStatistics);
        recording.enable(EventNames.FileWriteIOStatistics);
        recording.start();
        useRandomAccessFileWrite(tmp);
        useRandomAccessFileRead(tmp);
        useFileStreamWrite(tmp);
        useFileStreamRead(tmp);
        recording.stop();
        checkForEventAttributesEnabled(recording);
    }

    private static void useRandomAccessFileWrite(File tmp) throws Throwable {
        tmp.delete();
        try (RandomAccessFile ras = new RandomAccessFile(tmp, "rw")) {
            ras.write(writeInt);
            ras.write(writeBuf);
            accWriteExpected = accWriteExpected + writeBuf.length;
        }
    }

    private static void useRandomAccessFileRead(File tmp) throws Throwable {
        try (RandomAccessFile ras = new RandomAccessFile(tmp, "rw")) {
            int readInt = ras.read();
            byte[] readBuf = new byte[writeBuf.length];
            int readSize = ras.read(readBuf);
            accReadExpected = accReadExpected + readSize;
        }
    }

    private static void useFileStreamWrite(File tmp) throws Throwable {
        tmp.delete();
        try (FileOutputStream fos = new FileOutputStream(tmp)) {
            fos.write(writeInt);
            fos.write(writeBuf);
            accWriteExpected = accWriteExpected + writeBuf.length;
        }
    }

    private static void useFileStreamRead(File tmp) throws Throwable {
        try (FileInputStream fis = new FileInputStream(tmp)) {
            int readInt = fis.read();
            byte[] readBuf = new byte[writeBuf.length];
            int readSize = fis.read(readBuf);
            accReadExpected = accReadExpected + readSize;
        }
    }

    private static void checkForEventAttributesEnabled(Recording recording) throws Throwable {
        boolean hasReadValue = false;
        boolean hasWriteValue = false;
        String accWrite = null;
        String accRead = null;
        List<RecordedEvent> events = Events.fromRecording(recording);
        // First check : Atleast one event is created.
        Events.hasEvents(events);
        Events.hasEvent(events, "jdk.FileWriteIOStatistics");
        Events.hasEvent(events, "jdk.FileReadIOStatistics");

        for (RecordedEvent event : events) {
            System.out.println("The eventtype is are" + event.getEventType().getName().toString());
            if (event.getEventType().getName().toString().equals("jdk.FileWriteIOStatistics")) {
                String writeRateVal = Events.assertField(event, "writeRate").getValue().toString();
                accWrite = Events.assertField(event, "accWrite").getValue().toString();
                if (Double.parseDouble(writeRateVal) > 0) {
                    hasWriteValue = true;
                }
            } else if (event.getEventType().getName().toString().equals("jdk.FileReadIOStatistics")) {
                String readRateVal = Events.assertField(event, "readRate").getValue().toString();
                accRead = Events.assertField(event, "accRead").getValue().toString();
                if (Double.parseDouble(readRateVal) > 0) {
                    hasReadValue = true;
                }
            }
        }
        assertEquals(hasWriteValue, true);
        assertEquals(hasReadValue, true);
        assertGreaterThanOrEqual(Integer.parseInt(accWrite), accWriteExpected,
                "The accumulated write bytes should be equal or more that expected length");
        assertGreaterThanOrEqual(Integer.parseInt(accRead), accReadExpected,
                "The accumulated read bytes should be equal or more that expected length");
    }
}
