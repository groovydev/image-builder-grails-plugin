class ImageBuilderGrailsPlugin {
    // the plugin version
    def version = "0.2"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.7 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp", "test/**"
    ]

    def author = "Karol Balejko"
    def authorEmail = "kb@groovydev.org"
    def title = "A simple image builder."
    def description = 'A simple image builder grails plugin'

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/image-builder"

    def license = "APACHE"
    def issueManagement = [ system: "github", url: "https://github.com/groovydev/image-builder-grails-plugin/issues" ]
    def scm = [ url: "https://github.com/groovydev/image-builder-grails-plugin" ]
    
}
