########################################################################################
# Other environment variables:
# HOME /home/jetty
# JAVA_HOME /docker-java-home/jre
########################################################################################

FROM jetty:9.4.14-jre8

########################################################################################
# Ensure jetty user has proper permissions for the database volume:
########################################################################################

USER root
RUN mkdir /db && \
        mkdir /db/lucene-database && \
        /bin/bash -c 'chown -R jetty /db' && \
        /bin/bash -c 'chmod -R +rw /db'
USER jetty
RUN /bin/bash -c 'chmod -R +rw /db'

########################################################################################
# Set various environment variables to default values.
########################################################################################

ENV JETTY_HOME /usr/local/jetty
ENV JETTY_BASE /var/lib/jetty
ENV TMPDIR /tmp/jetty

ENV strategia_kartta_wiki_address ""
ENV strategia_kartta_https_wiki_address ""
ENV strategia_kartta_smtp_localhost www.digitulosohjaus.fi
ENV strategia_kartta_smtp_host -
ENV strategia_kartta_smtp_from strategia@digitulosohjaus.fi
ENV strategia_kartta_base_directory /db
ENV strategia_kartta_target_website_link ""
ENV strategia_kartta_database_id database
ENV strategia_kartta_receiver_email stkartta@gmail.com
ENV strategia_kartta_sender_email strategiakartta@simupedia.com
ENV strategia_kartta_guest_group_name GuestGroup
ENV strategia_kartta_guest_account_name Guest
ENV strategia_kartta_guest_account_password ""
ENV strategia_kartta_admin_group_name SystemGroup
ENV strategia_kartta_admin_account_name System
ENV strategia_kartta_admin_account_password ""
ENV strategia_kartta_admin_account_email contact@semantum.fi
ENV strategia_kartta_wiki_prefix Strategiakartta_
ENV strategia_kartta_printing_dir_name printing
ENV strategia_kartta_chapter_config 9:1,2,3
ENV strategia_kartta_max_short_description_length 150
ENV strategia_kartta_controller_account_name Tulosohjaajat
ENV strategia_active_years 2016,2017,2018,2019,2020
ENV strategia_kartta_max_comment_deletion_hours 24
ENV strategia_kartta_table_input_year_restriction_on true

########################################################################################
# Copy the pre-built .war to webapps root
########################################################################################

EXPOSE 8080

COPY ./releng/fi.semantum.strategia.war /var/lib/jetty/webapps/root.war
COPY ./fi.semantum.strategia/WebContent/phantomjs-linux-x86_64 /db/phantomjs-linux-x86_64

