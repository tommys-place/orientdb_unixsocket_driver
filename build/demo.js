var OrientDB = require('orientjs');

var server = OrientDB({
  host: 'unix:/tmp/orientdb.sock',
  username: 'root',
  password: 'root'
});

/*
server.list()
.then(function (dbs) {
  console.log('There are ' + dbs.length + ' databases on the server.');
});
*/

var db = server.use({
       name : 'demo',
       username: 'root',
       password: 'root'
});


db.query('select count(*) from Country').then(function(){
  console.log(arguments);
}).catch(function(){
   console.log(arguments);
}).done();

