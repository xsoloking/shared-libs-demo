package com.yusys.pipeline.utils

import groovy.json.JsonBuilder

class MetaData {
    def name
    def value

    MetaData(name, value) {
        this.name = name
        this.value = value
    }

    @NonCPS
    @Override
    String toString() {
        return new JsonBuilder(["name": name, "value": value]).toString()
    }
}