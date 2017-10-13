Process p = execute("pushd helloworld-ws && ./mvnw test && popd")
assert p.exitValue() == 0