*     Oracle - initial API and implementation
******************************************************************************/
package org.eclipse.jpt.jpa.eclipselink.core.internal.context.orm;

import org.eclipse.jpt.common.core.internal.utility.JavaProjectTools;
public Iterable<String> getCompletionProposals(int pos) {
Iterable<String> result = super.getCompletionProposals(pos);
if (result != null) {

