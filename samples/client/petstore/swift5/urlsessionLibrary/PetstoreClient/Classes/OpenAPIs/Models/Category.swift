//
// Category.swift
//
// Generated by openapi-generator
// https://openapi-generator.tech
//

import Foundation

public struct Category: Codable {

    public var id: Int64?
    public var name: String = "default-name"

    public init(id: Int64? = nil, name: String = "default-name") {
        self.id = id
        self.name = name
    }

}
