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
    
EsN0dB = respcs.EsN0dB(j);

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
    respcs.Nsim               = zeros(1,numel(respcs.EsN0dB));
    respcs.NErrActual = zeros(1,numel(respcs.EsN0dB));
    respcs.NErrCS     = zeros(1,numel(respcs.EsN0dB));
    respcs.NErrMissed = zeros(1,numel(respcs.EsN0dB));
    respcs.NErrFalse = zeros(1,numel(respcs.EsN0dB));
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

respcs.Nsim(respcs.EsN0dB == EsN0dB) = respcs.Nsim(respcs.EsN0dB == EsN0dB) + Nsim;
respcs.NErrActual(respcs.EsN0dB == EsN0dB) = respcs.NErrActual(respcs.EsN0dB == EsN0dB) + NErrActual;
respcs.NErrCS(respcs.EsN0dB == EsN0dB) = respcs.NErrCS(respcs.EsN0dB == EsN0dB) + NErrCS;
respcs.NErrMissed(respcs.EsN0dB == EsN0dB) = respcs.NErrMissed(respcs.EsN0dB == EsN0dB) + NErrMissed;
respcs.NErrFalse(respcs.EsN0dB == EsN0dB) = respcs.NErrFalse(respcs.EsN0dB == EsN0dB) + NErrFalse;

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
        yCS = yCS .* ysum;
        yCS = dec2bin(yCS,16);
        yCS = bin2dec(yCS(1:16));
    end
    yCS = dec2bin(yCS,16);
    f = yCS(1:16) == '1';
end
