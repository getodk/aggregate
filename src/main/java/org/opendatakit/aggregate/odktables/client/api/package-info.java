/**
 * This package contains classes for interacting with Aggregate through the
 * odktables API. This is the main entry point for clients of the API.
 *
 * Some definitions:
 * <ul>
 * <li>*ID (e.g. userID, tableID, rowID): IDs are identifiers of a resource that are at least unique to the client. These are generally generated and used by the client to internally keep track of resources the client owns. Note: userIDs are a special case in that they must be universally unique.</li>
 * <li>*UUID (e.g. userUUID, rowUUID): UUIDs are identifiers of a resource that are universally unique among all clients. Aggregate will generate and keep track of resources using UUIDs, and usually the client must identify resources to Aggregate using UUIDs.</li>
 * </ul>
 */
package org.opendatakit.aggregate.odktables.client.api;
