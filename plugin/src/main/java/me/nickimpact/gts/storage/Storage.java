/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.nickimpact.gts.storage;

import me.nickimpact.gts.api.listings.Listing;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Storage {

    String getName();

    /**
     * This method is to initialize our storage provider, loading anything necessary
     * about the provider.
     */
    void init();

    /**
     * This method is to ensure we properly close our connection when the plugin is either
     * no longer in use or in the middle of server shutdown.
     */
    void shutdown();

	/**
	 * Add a listing to the storage provider
	 *
	 * @param listing The target listing
	 * @return A completable future in which a listing is added to the storage provider
	 */
	CompletableFuture<Void> addListing(Listing listing);

	CompletableFuture<Void> updateListing(Listing listing);

	/**
	 * Remove a listing from the storage provider based on the ID of a listing
	 *
	 * @param uuid The target ID of a listing
	 * @return A completable future in which a listing is removed from the storage provider
	 */
    CompletableFuture<Void> removeListing(UUID uuid);

    /**
     * Fetches a list of all listings from the storage provider, and forms them into a list
     * to represent the cached listings.
     */
    CompletableFuture<List<Listing>> getListings();

	CompletableFuture<Void> addIgnorer(UUID uuid);

	CompletableFuture<Void> removeIgnorer(UUID uuid);

	/**
	 * Fetches a list of UUIDs that prefer not to be bothered by GTS broadcasts
	 *
	 * @return A set of users ignoring GTS broadcasts
	 */
	CompletableFuture<List<UUID>> getIgnorers();

    /**
     * This method is meant to clean out the gts, along with logs if the passed variable
     * is <code>true</code>.
     *
     * @return <code>true</code> on successful purge, <code>false</code> otherwise
     */
    CompletableFuture<Void> purge(boolean logs);

    /**
     * Attempts to save all data awaiting an update to the storage provider. However, for flatfile,
     * we will need to ensure we have all data present at time of save to ensure all is saved
     * properly.
     *
     * @return <code>true</code> on successful save, <code>false</code> otherwise
     */
    CompletableFuture<Void> save();
}
