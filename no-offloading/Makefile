mkfile_path := $(abspath $(lastword $(MAKEFILE_LIST)))
curr_dir := $(patsubst %/,%,$(dir $(mkfile_path)))


onos_url := http://localhost:8181/onos
onos_curl := curl --fail -sSL --user onos:rocks --noproxy localhost




run_exp_complete:
	make p4_compile
	make mvn_ci
	make mvn_pack
	make run_exp
run_exp:
	sudo rm tmp/zenoh_shm_pid_shmauth.* || echo "run"
	sudo rm tmp/*.txt || echo "run"
	make _start
	sleep 60
	make pcap-all
	make app-install
	sleep 60
	make push-netconf-fullCase
	sleep 60




prepare: mvn_ci mvn_pack

mvn_ci:
	[ ! -d "${curr_dir}/zenoh_app/src/main/resources" ] && mkdir ${curr_dir}/zenoh_app/src/main/resources
	cp -r ${curr_dir}/config zenoh_app/src/main/resources
	cp -r ${curr_dir}/p4/p4c-out/ zenoh_app/src/main/resources
	cd zenoh_app && mvn clean install

mvn_pack:
	cd zenoh_app && mvn org.onosproject:onos-maven-plugin:2.2:app




start:
	$(info *** Starting BMv2, ONOS and Hosts)
	docker-compose up -d



stop:
	docker-compose down
	@echo "Containers are down"


p4_compile:
	cd p4 && make fabric
	@echo


app-install:
	$(info *** Installing and activating app in ONOS...)
	${onos_curl} -X POST -HContent-Type:application/octet-stream \
		'${onos_url}/v1/applications?activate=true' \
		--data-binary @zenoh_app/target/zenoh_app-1.0.0-SNAPSHOT.oar
	@echo




app-uninstall:
	$(info *** Uninstalling app from ONOS (if present)...)
	-${onos_curl} -X DELETE ${onos_url}/v1/applications/org.onosproject.zenoh_app
	@echo

app-reload: app-uninstall app-install



push-netconf-fullCase: CONF := netcfg_fullCase.json
push-netconf-fullCase: push-netconf


push-netconf:
	$(info *** Pushing ${CONF} to ONOS...)
	${onos_curl} -X POST -H 'Content-Type:application/json' \
		${onos_url}/v1/network/configuration -d@./config/${CONF}
	@echo


onos-cli:
	$(info *** Connecting to the ONOS CLI...)
	$(info *** Top exit press Ctrl-D)
	@ssh -o "UserKnownHostsFile=/dev/null" -o "StrictHostKeyChecking=no" -o LogLevel=ERROR -p 8101 onos@localhost

onos-log:
	docker logs onos

mn-cli:
	$(info *** Attaching to Mininet CLI...)
	$(info *** To detach press Ctrl-D (Mininet will keep running))
	-@docker attach --detach-keys "ctrl-d" $(shell docker-compose ps -q mininet) || echo "*** Detached from Mininet CLI"

pcap:
	docker exec -it mininet /mininet/host-cmd \
	$(host) tcpdump -i $(host)-eth$(iface) \
	-U -w /tmp/$(host)-eth$(iface).pcap


pcap-all:
	cd scripts && python3 captures.py
