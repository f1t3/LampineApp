function f = pcs16(esnostart, esnostop, nsim)

reset = 0;

WordLen = 16;

for j = esnostart+201 : 1 : esnostop+201
    
results = loadres('resultspcs16.mat');
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

storeres('resultspcs16', results, esnodb, nsim, nerractual, nerrdet, nerrmiss, nerrfalse)

end

f = T/nsim;

end

function f = calccs(word)
    yCS = 1;
    csum = 7;
    for n = 1:numel(word(:,1))
        worddec = bin2dec(num2str(word(n,:)));
        csum = csum + worddec;
        yCS  = mod(yCS + csum, 2.^32);
        yCS  = dec2bin(yCS,16);
        yCS  = bin2dec(yCS(1:16));
        yCS  = mod(yCS .* csum, 2.^32);
        yCS  = dec2bin(yCS,16);
        yCS  = bin2dec(yCS(1:16));
    end
    yCS = dec2bin(yCS,16);
    f = yCS(1:16) == '1';
end
