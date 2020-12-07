/**
 * Copyright 2020 Matthew Layton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.onixlabs.corda.identityframework.contract

import net.corda.core.contracts.StateAndRef

/**
 * Determines whether the current state is pointing to the specified state.
 *
 * @param stateAndRef The state and ref of the state being pointed to.
 * @return Returns true if the current state is pointing to the specified state; otherwise, false.
 */
fun <T : ChainState> T.isPointingTo(stateAndRef: StateAndRef<T>): Boolean {
    return stateAndRef.ref == previousStateRef
}
