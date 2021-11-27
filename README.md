# rss-parser-lambda
AWS Lambda wrapper for rss-parser

This is an AWS Lambda for rss-parseer, which is here: https://github.com/kevinhooke/rss-parser

When called with a url to an RSS feed, this Lambda together with rss-parser extracts titles of
news articles and returns in a simpel json response that can be displayed by a text client.

The original use for this RSS parser is an Amateur Radio ax.25 node for Packet Radio, that allows
a user connecting to the node to request display of a list of text headlines from a parsed RSS
feed. Since this needs to be text only, the parser strips most html and simplifies the response
to be returned to a packet radio client.

The ax.25 node and related helper scripts are here: https://github.com/kevinhooke/packet-radio-node-scripts 
