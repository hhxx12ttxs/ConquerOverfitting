     * Opens the dialog for the specified complex.
     * @deprecated This method is not supported by this dialog. Use the
     * {@link ChangePricesDialog#open(Complex)} method instead.
     */
        {
            // Ignore ware if not used by the complex
            if (!complex.usesWare(ware)) continue;
            // Get price and check if ware is used
            int price;
            final boolean used;
            if (this.customPrices.containsKey(ware))
            {
     * 
    /**

