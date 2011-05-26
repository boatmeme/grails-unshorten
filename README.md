The Unshorten plugin provides a means for your Grails application to expand Shortened URLs (http://bit.ly/jkD0Qr and http://tinyurl.com/3vy9xga, for example) into their Original, Unshortened form (http://amazon.com or http://grails.org, for example) without the need for calling a Third-Party API.

Provides a Grails Service, TagLib, and Controller to your application.

Backed by a configurable Least-Recently-Used (LRU) Cache, the Unshorten plugin optimizes performance by making a minimal number of HTTP HEAD method calls.

Contains service calls and controller actions to dynamically handle single or multiple URLs as well as GSP Tags to unshorten URLs within blocks of text, and to create links