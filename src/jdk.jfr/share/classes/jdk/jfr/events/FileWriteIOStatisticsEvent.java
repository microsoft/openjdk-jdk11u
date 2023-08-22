/*
 * Copyright (c) 2012, 2018, Oracle and/or its affiliates. All rights reserved.
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

package jdk.jfr.events;

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;
import jdk.jfr.internal.Type;
import java.util.concurrent.atomic.AtomicLong;

@Name(Type.EVENT_NAME_PREFIX + "FileWriteIOStatistics")
@Label("FileWriteIO Statistics")
@Category({ "Java Application", "Statistics" })
@Description("Write Rate from the FileOutputStream, FileChannelImpl, RandomAccessFile")
@StackTrace(false)
public final class FileWriteIOStatisticsEvent extends AbstractJDKEvent {

    public static final ThreadLocal<FileWriteIOStatisticsEvent> EVENT = new ThreadLocal<>() {
        @Override
        protected FileWriteIOStatisticsEvent initialValue() {
            return new FileWriteIOStatisticsEvent();
        }
    };

    private static AtomicLong totalWriteBytesForProcess = new AtomicLong(0);
    private static AtomicLong totalWriteBytesForPeriod = new AtomicLong(0);
    private static AtomicLong totalDuration = new AtomicLong(0);

    @Label("Write Rate (Bytes per Sec)")
    public long writeRate;

    @Label("Total Accumulated Write Bytes Process")
    public long accWrite;

    // getters and setters
    public static long getTotalWriteBytesForProcess() {
        return totalWriteBytesForProcess.get();
    }

    public static void setTotalWriteBytesForProcess(long bytesWritten) {
        totalWriteBytesForProcess.addAndGet(bytesWritten);

    }

    public static long getTotalDuration() {
        return totalDuration.get();
    }

    public static long getTotalWriteBytesForPeriod() {
        return totalWriteBytesForPeriod.get();
    }

    public static long setTotalWriteBytesForPeriod(long bytesWritten, long duration) {
        totalDuration.addAndGet(duration);
        return totalWriteBytesForPeriod.addAndGet(bytesWritten);
    }

    public static long getWriteRateForPeriod() {
        // decrement the value with the get the gettotal
        long result = getTotalWriteBytesForPeriod();
        long interval = getTotalDuration();
        totalWriteBytesForPeriod.addAndGet(-result);       
        if (interval > 0) {
            totalDuration.addAndGet(-interval);
            double intervalInSec = (interval * 1.0 / 1000000000);           
            long wRate = (long) (result / intervalInSec);                  
            return wRate;
        }
        return 0;
    }
}
