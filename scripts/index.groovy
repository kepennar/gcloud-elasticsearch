

import groovy.json.JsonSlurper

import java.nio.charset.Charset;

@Grab(group='org.elasticsearch', module='elasticsearch-lang-groovy', version='2.0.0')
@GrabExclude('org.codehaus.groovy:groovy-all')

import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.action.bulk.BulkRequestBuilder
import org.elasticsearch.action.bulk.BulkResponse

	
	def error = System.err.&println
	def cli = new CliBuilder(usage: 'groovy index -[hrpc] [data]')
	cli.with {
		h longOpt: 'help',			'Show usage information'
		r longOpt: 'remote', 		args: 1, argName: 'remote',	'Remote host (default "localhost")'
		p longOpt: 'port',  		args: 1, argName: 'port', '	Remote port (default "9300")'
		c longOpt: 'cluster', 		args: 1, argName: 'cluster', 'Cluster name (default none)'
	}
	def options = cli.parse(args)
	
	if (!options) {
		System.exit(0)
	}
	if (options.h) {
		cli.usage()
		System.exit(0)
	}
	
	def args = options.arguments()
	if (!(args && args[0])) {
		error 'Invalid arguments! You have to define data path. Have a look on "groovy index-client -h"'
		System.exit(0)
	}
	def dataPath = args[0]
	
	// Default host
	def host = 'localhost'
	if (options.r) {
		host = options.r
	}
	// Default port
	def port = 9300
	if (options.p) {
		port = options.p
	}
	
	println "Connecting on '${host}:${port}'"
	def client
	if (options.c) {
		def settings = ImmutableSettings.settingsBuilder()
				.put("cluster.name", options.c).build();
		client = new TransportClient(settings);
	} else {
		client = new TransportClient()
	}
	client.addTransportAddress(new InetSocketTransportAddress(host, port));
	
	// Read datas
	def baseDir = System.getProperty("user.dir");
	def filePath = "${baseDir}/${dataPath}"
	println "Read ${filePath} data file"
	def content = new JsonSlurper().parse(new File(filePath), 'UTF-8')
	def indexName = content.indexName
	def type = content.type
	def datas = content.datas
	
	println "Begin indexing ${type} on ${indexName} index"

	// Bulk de 10000
	def bulkSize = 10000
	def nbElems = datas.size()
	def nbChunk = ((nbElems / bulkSize) as Integer) +1
	def index = 0

	println "Elements : ${nbElems}"
	println "Chunks : ${nbChunk}"

	for ( i in 0..nbChunk ) {
		def maxInBulk = ((i+1) * bulkSize) -1
		BulkRequestBuilder bulkRequest = client.prepareBulk();
		println "Index : ${index}"
		println "maxInBulk : ${maxInBulk}"
		while (index <= maxInBulk && index < nbElems ) {
			bulkRequest.add(
				client.prepareIndex(indexName, type)
					.setSource(datas[index].toString())
			);
			index++
		}
		BulkResponse bulkResponse = bulkRequest.execute().actionGet();
		//if (bulkResponse.hasFailures()) {
		//	error bulkResponse.buildFailureMessage()
		//	System.exit(0)
		//}
	}
	// on shutdown
	client.close()