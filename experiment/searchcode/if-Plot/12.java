public abstract boolean canModify(Plot plot, PlotPlayer player);

/**
* The plot may be null if the user is not standing in a plot. Return false if this is not a plot-less inbox.
* <br>
* The `whenDone` parameter should be executed when it&#39;s done fetching the comments.

