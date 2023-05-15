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

@Name(Type.EVENT_NAME_PREFIX + "FileReadIOStatistics")
@Label("FileReadIO Statistics")
@Category({ "Java Application", "Statistics" })
@Description("Read Rate from the FileInputStream, FileChannelImpl, RandomAccessFile")
@StackTrace(false)
public final class FileReadIOStatisticsEvent extends AbstractJDKEvent {

    public static long oldTimeStamp;
    private static AtomicLong totalReadBytes = new AtomicLong(0);

    @Label("Read Rate (Bytes/Sec)")
    public long readRate;

    public static long getTotalReadBytes() {
        return totalReadBytes.get();

    }

    public static long setAddReadBytes(long bytesRead) {
        return totalReadBytes.addAndGet(bytesRead);
    }

    public static long getandresettb() {
        // decrement the value with the get the gettotal
        long result = getTotalReadBytes();
        totalReadBytes.addAndGet(-result);
        return result;
    }


}
