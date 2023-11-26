# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do |config|
    #box to build off with ubuntu 20.04 lts
    config.vm.box = "ubuntu/focal64"

    #config ip of the machine
    # config.vm.network :private_network, ip: "192.168.58.111"

    #set vm as accesible in the local network
    config.vm.network "public_network"

    # Configuration for Virtual Box
    config.vm.provider :virtualbox do |vb|
        # Change the memory here if needed - 3 Gb memory on Virtual Box VM
        vb.customize ["modifyvm", :id, "--memory", "8072", "--cpus", "4"]
        # Change this only if you need destop for Ubuntu - you will need more memory
        vb.gui = false
    end
end