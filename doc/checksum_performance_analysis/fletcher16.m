close all, clear all
tic

reset = 0
Nsim = 10000;
EsN0dB = 0;

WordLen = 16;

EsN0start = 8;
EsN0stop  = 9;

ressum.EsN0dB = -200:200;
respcs.EsN0dB = -200:200;
rescrc.EsN0dB = -200:200;
resflet.EsN0dB = -200:200;

Nword = 0;

for j = EsN0start+201 : 1 : EsN0stop+201
    
EsN0dB = resflet.EsN0dB(j);

% Derivatives
Es = 0.5;
EsN0 = 10^(EsN0dB/10);
N0 =  Es/(EsN0);
    
NErrActual = 0;
NErrCS = 0;
NErrMissed = 0;
NErrFalse = 0;
NErrCRC = 0;

loadres;

if reset == 1
    resflet.Nsim       = zeros(1,numel(resflet.EsN0dB));
    resflet.NErrActual = zeros(1,numel(resflet.EsN0dB));
    resflet.NErrCS     = zeros(1,numel(resflet.EsN0dB));
    resflet.NErrMissed = zeros(1,numel(resflet.EsN0dB));
    resflet.NErrFalse = zeros(1,numel(resflet.EsN0dB));
end


for i = 1:Nsim
    % Generate random word
    x = randi([0,1], WordLen, 8);
    % Generate checksums
    xCS = calcflet(x);
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
    yCS = calcflet(y);
    % Check if actual word error occured
    e = sum(sum((y ~= x),2),1);
    e = e > 0;
    NErrActual = NErrActual + e;
    % Compare CS, check if word error would be detected
    eCS = sum(yCSrx ~= yCS);
    eCS = eCS > 0;
    NErrCS = NErrCS + eCS;
    % Check if error would be missed
    NErrMissed = NErrMissed + (eCS < e);
    % Check if false error
    NErrFalse = NErrFalse   + (eCS > e);
end

resflet.Nsim(resflet.EsN0dB == EsN0dB) = resflet.Nsim(resflet.EsN0dB == EsN0dB) + Nsim;
resflet.NErrActual(resflet.EsN0dB == EsN0dB) = resflet.NErrActual(resflet.EsN0dB == EsN0dB) + NErrActual;
resflet.NErrCS(resflet.EsN0dB == EsN0dB) = resflet.NErrCS(resflet.EsN0dB == EsN0dB) + NErrCS;
resflet.NErrMissed(resflet.EsN0dB == EsN0dB) = resflet.NErrMissed(resflet.EsN0dB == EsN0dB) + NErrMissed;
resflet.NErrFalse(resflet.EsN0dB == EsN0dB) = resflet.NErrFalse(resflet.EsN0dB == EsN0dB) + NErrFalse;

save('results.mat','respcs','ressum','rescrc','resflet')

Nword = Nword + Nsim;

end

T = toc
tavgPerWord = T/Nword

plotres;

function f = calcflet(word)
    sum1 = 0;
    sum2 = 0;
    for n = 1:numel(word(:,1))
        sum1 = sum1 + bin2dec(num2str(word(n,:)));
        sum1 = mod(sum1, 256);
        sum2 = sum2 + sum1;
        sum2 = sum2 + mod(sum2, 256);
    end
    sum1 = mod(sum1,256);
    sum2 = mod(sum2,256);
    flet = '0000000000000000';
    flet(1: 8) = dec2bin(sum1,8);
    flet(9:16) = dec2bin(sum2,8);
    f = flet == '1';
end

