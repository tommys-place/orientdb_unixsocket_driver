OrientDB UNIX Socket
==================

This is an add-on for [OrientDB](http://orientdb.com) database to enable UNIX socket connection.

####Reasons for unix socket usage :
> - Unix socket is much faster than TCP/IP and less memory demanding.
> - Unix socket is better choice when communicating with processes inside the same box
> - Support for [Nginx](http://nginx.org) unix socket proxying
> - Ideal for OrientDB in-memmory database (session storage)
> - [Docker](https://www.docker.com/) inter container communication through unix sockets instead of linked containers
> (linked containers will change IP address after restart)

####Features :
> - Zero-copy read/write operations
> - Pure Java implementation
> - TODO - Unix Domain Socket EPOLL - Async I/O

----------

Installation
--------------

#### <i class="icon-file"></i> Configure OrientDB

1. Copy jar file from ./build/orientdb/lib to installed OrientDB /lib folder
2. In /build/orientdb/config one can find setup example, search for **UNIX SOCKET SUPPORT** comment under sockets & listeners for an example.

Only parameter to setup inside xml configuration is **network.socket.unix** which should contain path to socket file. Default is */tmp/orientdb.sock*

---

#### <i class="icon-file"></i> Configure OrientJS

1. Create empty folder
2. Copy to that folder ./build/demo.js
3. Position to and execute

    > npm install orientjs

4. Open *./node_modules/orientjs/lib/transport/binary/connection.js* in text editor modify **createSocket** function

	**from**
	```js
	Connection.prototype.createSocket = function () {
		  var socket = net.createConnection(this.port, this.host);
		  socket.setNoDelay(true);
		  socket.setMaxListeners(100);
		  return socket;
	};
	```
	**to**
	```js
	Connection.prototype.createSocket = function () {
	    var socket = null;
	    var isUnix = ['linux','darwin'].indexOf(process.platform) > -1 ;
        if (this.host.indexOf('unix:') === 0 && isUnix) {
            var uxPath = this.host.replace('unix:','');
            socket = net.createConnection(uxPath);
	    } else {
	        socket = net.createConnection(this.port, this.host);
	    }
	    socket.setNoDelay(true);
	    socket.setMaxListeners(100);
	    return socket;
	};
	```

5. Whenever OrientDB driver is used inside Node.JS to connect to the unix socket, **unix:** prefix should be used.
	```js
	var OrientDB = require('orientjs');
	var server = OrientDB({
	    host: 'unix:/tmp/orientdb.sock',
	    username: 'root',
	    password: 'root'
	});
	```

6. Start app with command below to check if it is working
> node demo

---

> **Note:**

> - File *unixsocket.orientdb.jar* in **./build** folder is built with Java 8. If one have an older version of Java installed, Java sources should be rebuilt with that version.
> - Don't forget to include OrientDB jar's from it's lib folder before building.

|

> **Reference:**

> - Original project [junixsocket](https://github.com/kohlschutter/junixsocket)
> - [JNR project](https://github.com/jnr) part of [Java Panama project](http://openjdk.java.net/projects/panama/)

|

> **Performance analysis TCP/IP vs UNIX SOCKET:**

> - Binghampton University [analysis](http://osnet.cs.binghamton.edu/publications/TR-20070820.pdf)
> - Redis [benchmark](http://redis.io/topics/benchmarks)
> - MySQL [benchmark](http://brandon.northcutt.net/article/MySQL+Connection+Speed%26%2358%3B+Socket+VS+TCP%26%2347%3BIP/20140425.html)

|

> **Credits:**

> - Unix Domain Sockets in Java (AF_UNIX) [junixsocket](https://github.com/kohlschutter/junixsocket)

That's all folks!
