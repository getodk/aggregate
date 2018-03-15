#!/bin/sh -eux

# Delete all Linux headers
dpkg --list \
  | awk '{ print $2 }' \
  | grep 'linux-headers' \
  | xargs apt-get -y purge;

# Remove specific Linux kernels, such as linux-image-3.11.0-15 but
# keeps the current kernel and does not touch the virtual packages,
# e.g. 'linux-image-amd64', etc.
dpkg --list \
    | awk '{ print $2 }' \
    | grep 'linux-image-[234].*' \
    | grep -v `uname -r` \
    | xargs apt-get -y purge;

# Delete Linux source
dpkg --list \
    | awk '{ print $2 }' \
    | grep linux-source \
    | xargs apt-get -y purge;

# Delete ansible
apt-get -y purge ansible;

# Delete compilers
apt-get -y purge gcc-6 cpp-6;

# Delete development packages
dpkg --list \
    | awk '{ print $2 }' \
    | grep -- '-dev$' \
    | xargs apt-get -y purge;

# Delete unused packages
apt-get -y autoremove;
apt-get -y clean;

# Clean apt cache
rm -rf /var/cache/apt/*;
mkdir -p /var/cache/apt/archives/partial;

# Clean tmp
rm -rf /tmp/*;

# delete any logs that have built up during the install
find /var/log/ -name *.log -exec rm -f {} \;

# Create remove vagrant user, force new password on boot
cat > /etc/rc.local << EOL
#!/bin/sh -eux
deluser vagrant
rm -rf /home/vagrant
echo "root:aggregate" | chpasswd
chage -d 0 root
systemctl disable rc-local.service
rm /etc/sudoers.d/99_vagrant;
echo '#!/usr/bin/env bash' > /etc/rc.local
echo 'aggregate-update-issue' >> /etc/rc.local
aggregate-update-issue
EOL

# Install script
chmod +x /etc/rc.local
systemctl enable rc-local.service