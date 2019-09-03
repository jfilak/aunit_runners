import sapadt.AUnitRunner

def runner = new AUnitRunner("http", "localhost", 8000, "001", "DEVELOPER", "Down1oad")
println runner.executeForPackage("sool")
