function f = simnsec(sumtype, esnostart, esnostop, tsim)
    if contains(sumtype, 'flet')
        tword = fletcher16(9, 9, 1000);
        nsim = floor(tsim./tword/((esnostop-esnostart)+1));
        fletcher16(esnostart, esnostop, nsim);
        plotres;
    end
    if contains(sumtype, 'crc')
        tword = crc16(9, 9, 1000);
        nsim = floor(tsim./tword/((esnostop-esnostart)+1));
        crc16(esnostart, esnostop, nsim);
        plotres;
    end
end