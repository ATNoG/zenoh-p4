/*
 * Copyright 2016-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.pipelines.zenoh_fabric.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;

public class DeviceConfig{

    private final String devId;
    private String ipv4;
    private String mac;

    public DeviceConfig(String devId) {
        this.devId = devId;
    }




    public DeviceConfig(String devId, String mac, String ipv4) {
        this.devId = devId;
        this.ipv4 = ipv4;
        this.mac = mac;
    }

    public String getDevId() {
        return this.devId;
    }


    public String getIpv4() {
        return this.ipv4;
    }


    public String getMac() {
        return this.mac;
    }


    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof DeviceConfig)) {
            return false;
        }
        DeviceConfig deviceConfig = (DeviceConfig) o;
        return devId.equals(deviceConfig.devId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(devId);
    }

    @Override
    public String toString() {
        return "{" +
            " devId='" + getDevId() + "'" +
            ", ipv4='" + getIpv4() + "'" +
            ", mac='" + getMac() + "'" +
            "}";
    }


}
