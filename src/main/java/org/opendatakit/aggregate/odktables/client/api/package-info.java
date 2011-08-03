/**
 * This package contains classes for interacting with Aggregate through the
 * odktables API. This is the main entry point for clients of the API.
 *
 * Some definitions:
 * <ul>
 * <li>*ID (e.g. userID, tableID, rowID): IDs are identifiers of a resource that 
 * are at least unique to the client. These are generally generated and used by 
 * the client to internally keep track of resources the client owns. Note: userIDs 
 * are a special case in that they must be universally unique.</li>
 * <li>aggregate*Identifier (e.g. aggregateUserIdentifier, aggregateRowIdentifier, 
 * aggregateTableIdentifier): aggregate identifiers identify a resource in a way 
 * that is universally unique among all clients. Aggregate will generate and keep 
 * track of resources using these identifiers, and in certain cases the client must 
 * identify resources to Aggregate using aggregate identifiers.</li>
 * </ul>
 */
package org.opendatakit.aggregate.odktables.client.api;

