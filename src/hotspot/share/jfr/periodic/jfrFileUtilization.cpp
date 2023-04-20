/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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
 *
 */

#include "precompiled.hpp"
#include "jfr/jfrEvents.hpp"
#include "jfr/periodic/jfrFileUtilization.hpp"
#include "jfr/periodic/jfrOSInterface.hpp"
#include "jfr/utilities/jfrTime.hpp"
#include "jfr/utilities/jfrTypes.hpp"
#include "utilities/growableArray.hpp"
#include "runtime/os_perf.hpp"
#include <iostream>
using namespace std;

uint64_t old_read_bytes = 0;
uint64_t old_write_bytes = 0;

// If current counters are less than previous we assume the interface has been reset
// If no bytes have been either sent or received, we'll also skip the event
static uint64_t rates_per_second(uint64_t current, uint64_t old, const JfrTickspan& interval) {
 if(interval.value()>0){
    return ((current - old) * NANOSECS_PER_SEC) / interval.nanoseconds();
 } 
 return 0;
  
}


void JfrFileUtilization::send_events() {   
    ResourceMark rm; 
    static JfrTicks last_sample_instant;
    const JfrTicks cur_time = JfrTicks::now();
    const JfrTickspan interval = last_sample_instant == 0 ? cur_time - cur_time : cur_time - last_sample_instant;
    last_sample_instant = cur_time;
     FileIOInformationData* fio=new FileIOInformationData(0,0);
     uint64_t current_read_bytes = 0;
     uint64_t current_write_bytes = 0;
     uint64_t read_rate = 0;
     uint64_t write_rate = 0;

    const int ret_val =JfrOSInterface::fileIO_utilization(fio);
    if(fio!=NULL){
    current_read_bytes = fio->get_read_bytes();
    current_write_bytes = fio->get_write_bytes();
     }

    read_rate = rates_per_second(current_read_bytes, old_read_bytes, interval);
    write_rate = rates_per_second(current_write_bytes, old_write_bytes, interval);
    std::cout<<"read_rate:" << read_rate << " current_read_bytes:" << current_read_bytes << " old_read_bytes:" << old_read_bytes << " Interval:" << interval.seconds() << std::endl;
    std::cout<<"write_rate:" << write_rate << " current_write_bytes:" << current_write_bytes << " old_write_bytes:" << old_write_bytes << " Interval:" << interval.seconds() << std::endl;
     
    EventFileUtilization event(UNTIMED);    
    event.set_starttime(cur_time);
    event.set_endtime(cur_time);   
    event.set_readRate(read_rate);
    event.set_writeRate(write_rate);
    event.commit();
    old_read_bytes = current_read_bytes;
    old_write_bytes = current_write_bytes;

}


