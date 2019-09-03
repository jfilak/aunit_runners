package sapadt

import spock.lang.Specification

class AUnitRunnerTest extends Specification {
    def "AUnit Runner execute For Package" () {
        setup:
        def runner = new AUnitRunner("http", "localhost", 8000, "001", "DEVELOPER", "Down1oad")

        when:
        def result = runner.executeForPackage("sool")

        then:
        result != null
    }
}
