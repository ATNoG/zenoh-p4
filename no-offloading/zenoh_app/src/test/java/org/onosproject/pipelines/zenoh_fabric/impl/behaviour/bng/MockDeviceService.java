/*
 * Copyright 2019-present Open Networking Foundation
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
 * limitations under the License.%
 */

package org.onosproject.pipelines.zenoh_fabric.impl.behaviour.bng;

import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.MastershipRole;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.device.PortStatistics;

import java.util.List;

/**
 * Mock implmentation of DeviceService.
 */
class MockDeviceService implements DeviceService {
    @Override
    public int getDeviceCount() {
        return 0;
    }

    @Override
    public Iterable<Device> getDevices() {
        return null;
    }

    @Override
    public Iterable<Device> getDevices(Device.Type type) {
        return null;
    }

    @Override
    public Iterable<Device> getAvailableDevices() {
        return null;
    }

    @Override
    public Iterable<Device> getAvailableDevices(Device.Type type) {
        return null;
    }

    @Override
    public Device getDevice(DeviceId deviceId) {
        return null;
    }

    @Override
    public MastershipRole getRole(DeviceId deviceId) {
        return null;
    }

    @Override
    public List<Port> getPorts(DeviceId deviceId) {
        return null;
    }

    @Override
    public List<PortStatistics> getPortStatistics(DeviceId deviceId) {
        return null;
    }

    @Override
    public List<PortStatistics> getPortDeltaStatistics(DeviceId deviceId) {
        return null;
    }

    @Override
    public Port getPort(DeviceId deviceId, PortNumber portNumber) {
        return null;
    }

    @Override
    public boolean isAvailable(DeviceId deviceId) {
        return false;
    }

    @Override
    public String localStatus(DeviceId deviceId) {
        return null;
    }

    @Override
    public long getLastUpdatedInstant(DeviceId deviceId) {
        return 0;
    }

    @Override
    public void addListener(DeviceListener listener) {

    }

    @Override
    public void removeListener(DeviceListener listener) {

    }
}
