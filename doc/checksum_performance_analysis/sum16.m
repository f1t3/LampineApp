function f = sum16(esnostart, esnostop, nsim)

reset = 0;

WordLen = 16;

for j = esnostart+201 : 1 : esnostop+201
    
results = loadres('resultssum16.mat');
esnodb = results.esnodb(j);

% Derivatives
Es = 0.5;
EsN0 = 10^(esnodb/10);
N0 =  Es/(EsN0);
    
nerractual = 0;
nerrdet = 0;
nerrmiss = 0;
nerrfalse = 0;

if reset == 1
    results.esnodb      = zeros(1,numel(results.esnodb));
    results.nsim        = zeros(1,numel(results.esnodb));
    results.nerractual  = zeros(1,numel(results.esnodb));
    results.nerrdet     = zeros(1,numel(results.esnodb));
    results.nerrmiss    = zeros(1,numel(results.esnodb));
    results.nerrfalse   = zeros(1,numel(results.esnodb)); 
end

tic

for i = 1:nsim
    % Generate random word
    x = randi([0,1], WordLen, 8);
    % Generate checksums
    xCS = calccs(x);
    % Generate noise vector 
    w = sqrt(N0/2) .* randn(WordLen,8);
    wCS = sqrt(N0/2) .* randn(1,16);
    
    % TX signal
    s = x + w;
    sCS = xCS + wCS;
    
    % Hard decoding
    y = (s > 0.5);
    yCSrx = (sCS > 0.5);
    % Calculate checksums
    yCS = calccs(y);
    % Check if actual word error occured
    e = sum(sum((y ~= x),2),1);
    e = e > 0;
    nerractual = nerractual + e;
    % Compare CS, check if word error would be detected
    eCS = sum(yCSrx ~= yCS);
    eCS = eCS > 0;
    nerrdet = nerrdet + eCS;
    % Check if error would be missed
    nerrmiss = nerrmiss + (eCS < e);
    % Check if false error
    nerrfalse = nerrfalse   + (eCS > e);
end

T = toc;

storeres('resultssum16', results, esnodb, nsim, nerractual, nerrdet, nerrmiss, nerrfalse)

end

f = T/nsim;

end

function f = calccs(word)
    sum1 = 0;
    sum2 = 0;
    for n = 1:numel(word(:,1))
        sum1 = mod(sum1 + sum2 + bin2dec(num2str(word(n,:))), 255);
        sum2 = mod(sum2 + sum1, 255);
    end
    flet = '0000000000000000';
    flet(1: 8) = dec2bin(sum1,8);
    flet(9:16) = dec2bin(sum2,8);
    f = flet == '1';
end
