# A Strategy Planning Application
(Only Finnish language support)

# Development

### Prepare IDE

1) Download eclipse IDE (e.g. Eclipse Oxygen)
2) Open project
3) Download runjettyrun for Eclipse (use jetty 9+, since Java 8 has incompatibilities with Jetty 8, causing ArrayIndexOutOfBounds for class parsers!). (https://marketplace.eclipse.org/content/run-jetty-run)
4) Download Vaadin 7 plugin for Eclipse (http://vaadin.com/eclipse)

### Vaadin

0) Install vaadin (Vaadin 7, Plug-In version 4.0.2 for Eclipse). Might work with newer Vaadin 7 version too.
1) Right-click project -> "Properties"
2) Make sure "Project Facets" -> "Vaadin Plug-in for Eclipse" is checked

Optional 2.1) Make sure "Vaadin" -> "Suspend automatic widgetset build" is off
3) Resolve Ivy for your project (right-click -> Ivy -> Resolve)
4) Press the "Compile Vaadin Widgetsets" button in main Eclipse toolbar, when having selected your project
	- Can also be done with Ctrl + 6
	- Toolbar button is located next to "Run in Debug mode" icon and looks like a bunch of small blue squares

5) You can also build the Vaadin theme, button next to the "Compile Vaadin Widgetsets" button

### Run project locally

1) Right-click project -> "Run as" -> "Run Jetty"
2) Navigate to browser:
	http://localhost:8080/fi.semantum.strategia/#

Ensure "database.xlsx", "startPage.md" and "faq.md" is in your DB (e.g. .\strategia-db\) to populate with default map and types and start page

### Login

You'll see in "Database.java" that a username "System" with "" as password is set as a default admin for development.
A Guest account (non-admin) is "Guest" with "" as password.

### Design considerations

The application is a Vaadin servlet application with small amount of custom JS for rendering the strategy map. This means
that almost all application logic is executed as server side Java.

The data model if the application is stored as objects (Base.java) with properties and relations between objects. Each object is uniquely identified with an uuid and also a
short name and a longer text can be specified for an object. All objects are stored within a single object (Database.java) and persisted with standard Java serialization 
into file system. Old versions of the database are stored as files in the database directory. The program supports multiple databases in named directories.

The application only supports a single language (Finnish) and implementing a custom language requires a lot of refactoring in the way names of fields are defined.

### Code

Important classes:

* Main.java is a container for all session data 
* Utils.java and DBUtils.java contains some utilities
* Updates.java contains logic for managing database changes and session states
* Actions.java contains menu actions triggered from the strategy map (MapListener.java is the server side entrypoint)
* StrategyMap.java represents a strategy map. A corresponding MapVis.java is prepared whenever a map needs to be rendered and sent serialized to the JS side
* OuterBox.java represents an outer box in the map. A corresponding OuterBoxVis.java is prepared whenever a map needs to be rendered and sent serialized to the JS side
* InnerBox.java represents an inner box in the map. A corresponding InnerBoxVis.java is prepared whenever a map needs to be rendered and sent serialized to the JS side
* Account.java and AccountGroup.java represent the user's and their groups, and through the groups, their permissions

In Servlet.java "servletInitialized" is run first and thus your initialization code can be placed there.
Back-End objects are sent to Vaadin and parsed as JSON, which are then sent to the JavaScript side and shown to the user.
The Base.java class defined the abstract class which all database objects use.
Main.java is the Vaadin applications main, which contains all the various buttons and fields.

#### Plugins

There are a couple of plugins that together form the application:

```
fi.semantum.strategia (e.g. main plugin)
fi.semantum.strategia.contrib
fi.semantum.strategia.map.transformation
fi.semantum.strategia.map.views
fi.semantum.strategy.db
```

The idea is to allow different versions of the application to be built by changing the plugins. For example, the map.transformation is the plugin that should be changed
when custom content needs to be generated from the map in the Result Agreements. There, tags can be defined that are used in the Markdown parser to generate content based
on Map object's contents. The map transformations are often hardcoded to assume certain structure of the Maps, espeially their layers and the amount of them. A custom map
transformation plugin is needed for projects with different needs.


### Database

The database is stored under the project, in the directory called "strategia-db". You can delete this folder if you want a clean DB.

(e.g. (your git parent dir)\git\strategiakartta-master\fi.semantum.strategia\strategia-db\)

If you want to load an existing DB, make sure to place it under .\strategia-db\root\ and name it database

### JavaScript development in Eclipse?

Install from market place JavaScript development tools for your current version of eclipse.
How to make this actually show syntax errors, etc. in the JavaScript?

### Adding JS to your code:

1) Create a new JS file under /WebContent/VAADIN/js
2) Create a new Java class that "extends AbstractJavaScriptComponent"
3) Add to your Java class:
@JavaScript(value = {
		"app://VAADIN/js/<your js file>.js"
	})
4) Create a constructor for your Java and define the functions (se D3.java as an example)
5) Compile widget set
6) Add your JS code. See map.nocache.js as an example.

Note: Functions have global namespace, make sure the function names are unique between all JS files!

### CSS and Styles

Add new .css files to WebConent/Vaadin/themes/fi_semantum_strategia.
Then, add to styles.scss: @import url("sheets-of-paper-a4.css");
Where the string is your file.
Next, compile the vaadin widget theme.

Vaadin slider styles have been overriden in this project! See styles.scss .v-slider-feedback

JavaScript uses FontAwesome in many places. Consider the following code:

```
	element
	.style("font-family", "FontAwesome" ) //Set font-family to FontAwesome for ICONs in the TEXT field
    .text(function(d) { return "\uf090";}); //This is the fa-sign-in icon from FontAwesome!
```

View the current FontAwesome icons from the web and their codes. The current version of FontAwesome is 4.4.0.

# Usage

### General

To add new boxes to the strategy-map, you'll need to login and click the "Edit" button (an eye-icon)

### Startup configurations

The application uses environment variables when started. See the Dockerfile for an example, or Configuration.java and DBConfiguration.java for the available variables.

# Production

Ensure your .war is built correctly to include all projects. Go to project preferences / properties -> Deployment assembly.
There, include all the projects you want in your .war.

Export your .war from eclipse:

```
1) Right-click your project (fi.semantum.strategia)
2) Export...
3) Web/War file
4) Choose fi.semantum.strategia and select the ./releng directory in root project
5) Give the war the name "fi.semantum.strategia.war"
```

To see if a project is included (as a .jar) in your built .war, you can run:

```
$ jar tvf ./releng/fi.semantum.strategia.war | grep <name of project or part of the name>
```


Build Docker image from dockerfile:

```
cd <root with this readme)
docker build -t strategiakartta:latest .
```

Run the image:

```
(ensure you have a volume that contains a database.xlsx since the image expects a file there):
1) docker volume create db

2) cp ./database.xlsx /var/lib/docker/volumes/db/_data/database.xlsx
2.1) cp ./startPage.md /var/lib/docker/volumes/db/_data/startPage.md

3) chmod -R a+rw /var/lib/docker/volumes/db/_data/

4) docker run -p 80:8080 -u jetty -v db:/db strategiakartta:latest
```

To run as an automatically restarting docker swarm service (ensure database.xlsx is in volume just like above)

```
$ docker swarm init
(init DB volume here, see above)
$ docker service create -p 8080:8080 -u jetty --name kartta --mount type=volume,source=db,destination=/db strategiakartta:latest
```

You should setup nginx and redirect to the port you choose for your service.

## Production usage

```
Download new image:

$ docker pull repo.simupedia.com:18078/kartta:latest
(or registry.simupedia.com/strategikartta/kartta:latest)

Update service (note: this only works if the service has updated enabled),
by adding: --update-delay 1s during service creation.
$ docker service update kartta

Delete service:
$ docker service rm kartta

Look at logs (with optional --tail command):

First show container ID:
$ docker ps -aq

Then logs:
$ docker logs --tail XXX <container id>
```

#### Production usage On Cent OS (install docker, pull image, start service)

```
sudo yum check-update
sudo yum -y update
sudo yum clean all
sudo yum install yum-utils device-mapper-persistent-data lvm2
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo yum install docker-ce docker-ce-cli containerd.io
sudo systemctl start docker

Use either this:
sudo docker login https://registry.simupedia.com/strategikartta
		Username: ...
		Password: ...
sudo docker pull registry.simupedia.com/strategikartta/kartta:latest

OR this:

sudo docker login repo.simupedia.com:18078
		Username: ...
		Password: ...
sudo docker pull repo.simupedia.com:18078/kartta:latest


sudo docker volume create db
sudo cp ./database.xlsx /var/lib/docker/volumes/db/_data/database.xlsx
sudo cp ./startPage.md /var/lib/docker/volumes/db/_data/startPage.md
sudo chmod -R a+rw /var/lib/docker/volumes/db/_data/
sudo docker swarm init

Depending on if your used registry.simupedia.com or repo.simupedia.com earlier, deploy your service as:
sudo docker service create -p 8080:8080 -u jetty --name=kartta --mount type=volume,source=db,destination=/db --update-delay 1s repo.simupedia.com:18078/kartta:latest
OR as:
sudo docker service create -p 8080:8080 -u jetty --name=kartta --mount type=volume,source=db,destination=/db --update-delay 1s registry.simupedia.com/strategikartta/kartta:latest
```

The service will run on port 8080, make sure to redirect with nginx to this port.