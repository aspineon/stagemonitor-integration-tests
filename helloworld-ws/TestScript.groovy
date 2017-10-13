Process p = execute("cd helloworld-ws && ./mvnw test && cd ..")
assert p.exitValue() == 0