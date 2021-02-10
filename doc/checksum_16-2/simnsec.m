function f = simnsec(sumtype, esnostart, esnostop, tsim)
    if contains(sumtype, 'fletcher')
        tword = fletcher16(9, 9, 1000);
        nsim = floor(tsim./tword/((esnostop-esnostart)+1));
        fletcher16(esnostart, esnostop, nsim);
        plotres;
    end
end