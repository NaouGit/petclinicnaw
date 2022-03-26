FROM tomcat:8.5.51



ADD /target/petclinic.war  /usr/local/tomcat/webapps/



EXPOSE 8080