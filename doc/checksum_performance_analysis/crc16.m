close all, clear all
tic

reset = 0
Nsim = 100000;
EsN0dB = 0;

WordLen = 16;

EsN0start = 7;
EsN0stop  = 9;

ressum.EsN0dB = -200:200;
respcs.EsN0dB = -200:200;
rescrc.EsN0dB = -200:200;
resflet.EsN0dB = -200:200;

Nword = 0;

for j = EsN0start+201 : 1 : EsN0stop+201
    
EsN0dB = rescrc.EsN0dB(j);

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
    rescrc.Nsim       = zeros(1,numel(rescrc.EsN0dB));
    rescrc.NErrActual = zeros(1,numel(rescrc.EsN0dB));
    rescrc.NErrCS     = zeros(1,numel(rescrc.EsN0dB));
    rescrc.NErrMissed = zeros(1,numel(rescrc.EsN0dB));
    rescrc.NErrFalse = zeros(1,numel(rescrc.EsN0dB));
end

% CRC-8: Ignore highest 1
%gen  = [1, 1, 0, 1, 0, 0, 1, 1, 1]; % CRC-8-Bluetooth
%gen  = [1, 0, 1, 0, 1, 1, 1, 0, 1]; % x8 + x7 + x4 + x0
gen  = [1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1]; % x8 + x7 + x4 + x0


for i = 1:Nsim
    % Generate random word
    x = randi([0,1], 1, WordLen * 8);
    % Generate checksums
    crc = calccrc([x, zeros(1,numel(gen)-1)],gen);
    x = [x, crc];
    % Generate noise vector 
    w = sqrt(N0/2) .* randn(1, WordLen * 8 + numel(gen)-1);
    % TX signal
    s = x + w;
    % Hard decoding
    y = (s > 0.5);
    % Calculate checksums
    crc = calccrc(y, gen);
    % Check if actual word error occured (only in data!)
    e = sum(sum((y(1:WordLen*8) ~= x(1:WordLen*8)),2),1);
    e = e > 0;
    NErrActual = NErrActual + e;
    % Check CRC, check if word error would be detected
    eCS = sum(crc) ~= 0;
    NErrCS = NErrCS + eCS;
    % Check if error would be missed
    NErrMissed = NErrMissed + (eCS < e);
    % Check if false error
    NErrFalse = NErrFalse   + (eCS > e);
end

rescrc.Nsim(rescrc.EsN0dB == EsN0dB) = rescrc.Nsim(rescrc.EsN0dB == EsN0dB) + Nsim;
rescrc.NErrActual(rescrc.EsN0dB == EsN0dB) = rescrc.NErrActual(rescrc.EsN0dB == EsN0dB) + NErrActual;
rescrc.NErrCS(rescrc.EsN0dB == EsN0dB) = rescrc.NErrCS(rescrc.EsN0dB == EsN0dB) + NErrCS;
rescrc.NErrMissed(rescrc.EsN0dB == EsN0dB) = rescrc.NErrMissed(rescrc.EsN0dB == EsN0dB) + NErrMissed;
rescrc.NErrFalse(rescrc.EsN0dB == EsN0dB) = rescrc.NErrFalse(rescrc.EsN0dB == EsN0dB) + NErrFalse;

save('results.mat','respcs','ressum','rescrc','resflet')

Nword = Nword + Nsim;

end

T = toc
tavgPerWord = T/Nword

plotres;

function f = calccrc(word, gen)
    gentmp = [gen, zeros(1, numel(word)-numel(gen))];
    for bit = 1 : numel(word) - numel(gen) + 1
        if word(bit) ~= 0
            % MSB is 1
            word = xor(word,gentmp);
        end 
        gentmp(2:numel(gentmp)) = gentmp(1:numel(gentmp)-1);
        gentmp(1) = 0;
    end
    f = word(numel(word)-(numel(gen)-1)+1:numel(word));
end

