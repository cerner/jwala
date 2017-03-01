PUSHD  d:\jwala\apache-httpd-2.4.10
d:\jwala\apache-httpd-2.4.10\bin\httpd -k install -n Apache2.4 -f d:\jwala\app\data\httpd\httpd.conf
CMD /C SC config Apache2.4 DisplayName= "Apache Apache2.4"
POPD