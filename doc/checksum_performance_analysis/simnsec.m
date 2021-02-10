function f = simnsec(sumtype, esnostart, esnostop, tsim)
   
    if contains(sumtype, 'fletcher')
        tword = fletcher16(0, 0, 1000);
        nsim = floor(tsim./tword/((esnostop-esnostart)+1));
        fletcher16(esnostart, esnostop, nsim);
        plotres;
    end
  
    if contains(sumtype, 'crc')
        tword = crc16(0, 0, 1000);
        nsim = floor(tsim./tword/((esnostop-esnostart)+1));
        crc16(esnostart, esnostop, nsim);
        plotres;
    end

    if contains(sumtype, 'sum')
        tword = sum16(0, 0, 1000);
        nsim = floor(tsim./tword/((esnostop-esnostart)+1));
        sum16(esnostart, esnostop, nsim);
        plotres;
    end
    
    if contains(sumtype, 'pcs')
        tword = pcs16(0, 0, 1000);
        nsim = floor(tsim./tword/((esnostop-esnostart)+1));
        pcs16(esnostart, esnostop, nsim);
        plotres;
    end 
    
end