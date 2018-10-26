package ru.myandroid.drebedengi_my;


class BalanceStruct {
    int walletsNumber;
    int currenciesNumber;

    int[] walletHiddenData;
    int[][] balanceData;
    String[] currencyData;

    BalanceStruct(int ext_walletsNumber, int ext_currenciesNumber,
            int[] ext_walletHiddenData, int[][] ext_balanceData, String[] ext_currencyData) {

        walletsNumber = ext_walletsNumber;
        currenciesNumber = ext_currenciesNumber;

        walletHiddenData = ext_walletHiddenData;
        balanceData = ext_balanceData;
        currencyData = ext_currencyData;
    }
}
