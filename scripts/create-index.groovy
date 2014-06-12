    

    import groovy.util.CliBuilder
    @Grab(group='org.elasticsearch', module='elasticsearch-lang-groovy', version='2.0.0')
	@GrabExclude('org.codehaus.groovy:groovy-all')
	
	import org.elasticsearch.client.Client
	import org.elasticsearch.client.transport.TransportClient
    import org.elasticsearch.common.transport.InetSocketTransportAddress
    import org.elasticsearch.common.settings.ImmutableSettings


    def error = System.err.&println
    def cli = new CliBuilder(usage: 'groovy create-index -[hrpcs] [indexname')
    cli.with {
        h longOpt: 'help',			'Show usage information'
        r longOpt: 'remote', 		args: 1, argName: 'remote',	'Remote host (default "localhost")'
        p longOpt: 'port',  		args: 1, argName: 'port', '	Remote port (default "9300")'
        c longOpt: 'cluster', 		args: 1, argName: 'cluster', 'Cluster name (default none)'
        s longOpt: 'settings', 		args: 1, argName: 'settings', 'Indexation descriptor (default none)'
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
	println args
    if (!(args && args.size() == 1)) {
        error 'Invalid arguments! You have to define index name. Have a look on "groovy index-client -h"'
        System.exit(0)
    }
    def indexName = args[0]

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
	
	// Default : No specific settings
	def settings
	if (options.s) {
		settings = new File(options.s).text
	}

    println "Connecting on '${host}:${port}' node"
    Client client
    if (options.c) {
        def clientSettings = ImmutableSettings.settingsBuilder()
            .put("cluster.name", options.c).build();
        client = new TransportClient(clientSettings);
    } else {
        client = new TransportClient()
    }
    client.addTransportAddress(new InetSocketTransportAddress(host, port));

	def response = client.admin().indices()
		.prepareCreate(indexName)
		.setSettings(settings)
		.execute()
		.actionGet()
	
	println response
    // on shutdown
    client.close()