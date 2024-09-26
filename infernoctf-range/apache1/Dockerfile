FROM ubuntu:18.04

# Add the Ubuntu archive for older package versions
RUN apt-get update && \
    apt-get install -y software-properties-common && \
    add-apt-repository 'deb http://archive.ubuntu.com/ubuntu bionic main universe' && \
    apt-get update && \
    apt-get install -y apache2=2.4.29-1ubuntu4.1

EXPOSE 80
CMD ["apachectl", "-D", "FOREGROUND"]
