telecom_loglog_rate_graph
=========================

What is it
----------

Draw loglog graphs of the downstream/upstream rates of ISP services, whith particular regions in the final graph
highlighted. 

I didn't find an easy way to generate appropriate diagrams that would have needed extensive hand-editing and
post-processing anyway, thus this tool.

This is not full-featured application. It is a Groovy script that generates the diagrams as bitmaps and

   * Displays them in a primitive window
   * Writes them to disk

Input data for the diagrams can be found in class `eu.qleap.smc_uhd.rateplot.desc.RateData`. Additional flexibility
is implemented by editing the source code! 

How to run it
-------------

The program can run on any OS as long as the following is installed:

   * [Java JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
   * [Groovy](http://groovy.codehaus.org/), i.e. the 'groovy-all.jar' must be on the Java CLASSPATH (see the [Groovy Quickstart](http://groovy.codehaus.org/Quick+Start)
   * [SLF4J](http://www.slf4j.org/), i.e. the 'slf4-api.jar' must be on the Java CLASSPATH
   * Optionally, [Logback](http://logback.qos.ch/), i.e. the 'logback-classic.jar' and 'logback-core.jar' can also be on the Java CLASSPATH

Example output
--------------

These are reduced versions of the generated diagrams:

![Internet connection services marketed by 'Post Luxembourg'](https://raw.github.com/dtonhofer/telecom_loglog_rate_graph/master/imagesgraph.POST.small.png "Internet connection services marketed by 'Post Luxembourg'")

![Internet connection services marketed by Cable Operators](https://raw.github.com/dtonhofer/telecom_loglog_rate_graph/master/imagesgraph.CABLEOPERATORS.small.png "Internet connection services marketed by Cable-Operators")

License
-------

This tool was written while under contract by Q-LEAP but with extensive personal time invested, so the copyright is held by Q-LEAP S.A. but the license is MIT.

Distributed under the MIT License, see http://opensource.org/licenses/MIT

Copyright (c) 2013
Q-LEAP S.A.
14, rue Aldringen
L-1118 Luxembourg

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


