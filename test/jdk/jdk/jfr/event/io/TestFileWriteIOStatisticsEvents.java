/*
 * Copyright (c) 2018, 2020, Oracle and/or its affiliates. All rights reserved.
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;



import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordedEvent;
import jdk.test.lib.jfr.EventNames;
import jdk.test.lib.jfr.Events;
import jdk.test.lib.thread.TestThread;
import jdk.test.lib.thread.XRun;
import jdk.test.lib.Utils;
import static jdk.test.lib.Asserts.assertGreaterThanOrEqual;


/**
 * @test
 * @key jfr
 * @requires vm.hasJFR
 * @library /test/lib /test/jdk
 * @run main/othervm jdk.jfr.event.io.TestFileWriteIOStatisticsEvents
 */
public class TestFileWriteIOStatisticsEvents { 
    static String message = "Keerthi Log: Hello, world!";

    public static void main(String[] args) throws Throwable {        
        Recording recording = new Recording();        
        recording.enable(EventNames.FileWriteIOStatistics).withPeriod(Duration.ofMillis(1)); 
        createTestFileEvents(recording);
        checkForEventAttributesEnabled(recording);
    }    

    private static void createTestFileEvents(Recording recording) throws Throwable{       
        File tmp = Utils.createTempFile("TestFileIOStatistics", ".tmp").toFile();        
        recording.start();        
        try (FileWriter writer = new FileWriter(tmp)) {       
            writer.write(message);  
            Thread.sleep(100);            
                      
        } catch (IOException e) {
            System.err.println("Error writing to file " + tmp + ": " + e.getMessage());
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }        
        recording.stop();  
    }

    private static void checkForEventAttributesEnabled(Recording recording) throws Throwable
    {
        boolean hasValue = false;
        String accWrite = null;
         List<RecordedEvent> events = Events.fromRecording(recording);   
         //First check : Atleast one event is created.
        Events.hasEvents(events);

        for (RecordedEvent event : events) {                    
            String writeRateVal = Events.assertField(event, "writeRate").getValue().toString(); 
            accWrite = Events.assertField(event, "accWrite").getValue().toString();
            if(Integer.parseInt(writeRateVal) > 0){
                hasValue = true;            
            }            
        }
         assertEquals(hasValue,true);
         assertGreaterThanOrEqual(Integer.parseInt(accWrite), message.length(), "The accrate is atleast equal more than message length "+accWrite);
    }
}
