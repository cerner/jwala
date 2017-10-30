var newman = require('newman');
var http = require('http');
var fs = require('fs');

var args = process.argv.slice(2);

var host = args[0];
var userName = args[1];
var Password = args[2];
var targetOs = args[3];
var jdkUrl = args[4];
var tomcatUrl = args[5];
var apacheHttpdUrl = args[6];
var binariesDest = args[7];
var mediaRemoteDir = args[8];
var context = args[9];
var helloWorldWar = args[10];

var download = function(url, dest, cb) {
  var file = fs.createWriteStream(dest);
  var request = http.get(url, function(response) {
    response.pipe(file);
    file.on('finish', function() {
      file.close(cb);  // close() is async, call cb after close completes.
    });
  }).on('error', function(err) { // Handle errors
    fs.unlink(dest); // Delete the file async. (But we don't check the result)
    if (cb) cb(err.message);
  });
};

var downloadCallback = function(message) {
 if (message) {
    console.log(message);
 } else {
    console.log("file finished");
 }
};

var basename = function(path) {
    return path.split(/[\\/]/).pop();
}

var jdkDest = binariesDest + '/' + basename(jdkUrl);
var tomcatDest = binariesDest + '/' + basename(tomcatUrl);
var apacheHttpdDest = binariesDest + '/' + basename(apacheHttpdUrl);

download(jdkUrl, jdkDest, function(errMsg) {
    if (errMsg) {
        console.log("Failed to download JDK zip: " + errMsg);
        return;
    } else {
        download(tomcatUrl, tomcatDest, function(errMsg) {
            if (errMsg) {
                console.log("Failed to download Tomcat zip: " + errMsg);
                return;
            } else {
                download(apacheHttpdUrl, apacheHttpdDest, function(errMsg){
                    if (errMsg) {
                        console.log("Failed to download Apache HTTPD zip: " + errMsg);
                        return;
                    } else {
                        runPostman();
                    }
                });
            }
        });
    }
});

var runPostman = function() {
    var myEnvironment = require('./jwala.postman_environment.json');
    myEnvironment.values.push({
      "key": "host",
      "type": "text",
      "value": host,
      "enabled": true,
      "warning": ""
    });
    myEnvironment.values.push({
      "key": "userName",
      "type": "text",
      "value": userName,
      "enabled": true,
      "warning": ""
    });
    myEnvironment.values.push({
      "key": "Password",
      "type": "text",
      "value": Password,
      "enabled": true,
      "warning": ""
    });
    myEnvironment.values.push({
      "key": "targetOs",
      "type": "text",
      "value": targetOs,
      "enabled": true,
      "warning": ""
    });
    myEnvironment.values.push({
      "key": "jdkMediaSrc",
      "type": "text",
      "value": jdkDest,
      "enabled": true,
      "warning": ""
    });
    myEnvironment.values.push({
      "key": "tomcatMediaSrc",
      "type": "text",
      "value": tomcatDest,
      "enabled": true,
      "warning": ""
    });
    myEnvironment.values.push({
      "key": "apacheHttpdMediaSrc",
      "type": "text",
      "value": apacheHttpdDest,
      "enabled": true,
      "warning": ""
    });
    myEnvironment.values.push({
      "key": "mediaRemoteDir",
      "type": "text",
      "value": mediaRemoteDir,
      "enabled": true,
      "warning": ""
    });
    myEnvironment.values.push({
      "key": "context",
      "type": "text",
      "value": context,
      "enabled": true,
      "warning": ""
    });
    myEnvironment.values.push({
          "key": "helloWorldWar",
          "type": "text",
          "value": helloWorldWar,
          "enabled": true,
          "warning": ""
    });

    newman.run({
        collection: require('./jwala-collection.postman_collection.json'),
                    environment: myEnvironment,
                    insecure: true,
					bail: true,
        reporters: 'cli'
    }).on('start', function (err, args) { // on start of run, log to console
        console.log('running a collection...');
    }).on('done', function (err, summary) {
        if (summary.run.failures.length !== 0) {
            console.log('collection failed on test cases');
            process.exit(-1);
        }
        else {
            console.log('collection run completed.');
        }
    });
};