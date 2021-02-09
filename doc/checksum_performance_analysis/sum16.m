close all, clear all
tic

reset = 0
Nsim = 10000;
EsN0dB = 0;

WordLen = 16;

EsN0start = 8
EsN0stop  = 10;

ressum.EsN0dB = -200:200;
respcs.EsN0dB = -200:200;
rescrc.EsN0dB = -200:200;
resflet.EsN0dB = -200:200;

Nword = 0;

for j = EsN0start+201 : 1 : EsN0stop+201
    
EsN0dB = ressum.EsN0dB(j);

% Derivatives
Es = 0.5;
EsN0 = 10^(EsN0dB/10);
N0 =  Es/(EsN0);
    
NErrActual = 0;
NErrCS = 0;
NErrMissed = 0;
NErrFalse = 0;

loadres;

if reset == 1
    ressum.Nsim               = zeros(1,numel(ressum.EsN0dB));
    ressum.NErrActual = zeros(1,numel(ressum.EsN0dB));
    ressum.NErrCS     = zeros(1,numel(ressum.EsN0dB));
    ressum.NErrMissed = zeros(1,numel(ressum.EsN0dB));
    ressum.NErrFalse = zeros(1,numel(ressum.EsN0dB));
end

for i = 1:Nsim
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

ressum.Nsim(ressum.EsN0dB == EsN0dB) = ressum.Nsim(ressum.EsN0dB == EsN0dB) + Nsim;
ressum.NErrActual(ressum.EsN0dB == EsN0dB) = ressum.NErrActual(ressum.EsN0dB == EsN0dB) + NErrActual;
ressum.NErrCS(ressum.EsN0dB == EsN0dB) = ressum.NErrCS(ressum.EsN0dB == EsN0dB) + NErrCS;
ressum.NErrMissed(ressum.EsN0dB == EsN0dB) = ressum.NErrMissed(ressum.EsN0dB == EsN0dB) + NErrMissed;
ressum.NErrFalse(ressum.EsN0dB == EsN0dB) = ressum.NErrFalse(ressum.EsN0dB == EsN0dB) + NErrFalse;

save('results.mat','respcs','ressum','rescrc','resflet')

Nword = Nword + Nsim;

end

T = toc
tavgPerWord = T/Nword

plotres;

function f = calccs(word)
    yCS = 1;
    ysum = 1;
    for n = 1:numel(word(:,1))
        ysum = ysum + bin2dec(num2str(word(n,:)));
        ysum = mod(ysum,65536);
    end
    yCS = dec2bin(ysum,16);
    f = yCS(1:16) == '1';
end
