close all, clear all
tic

Nsim = 100;
EsN0dB = 0;

WordLen = 16;

EsN0start = 0;
EsN0stop  = 15;

EsN0dB_save = -200:200;

Nword = 0;

for j = EsN0start+201 : 1 : EsN0stop+201
    
EsN0dB = EsN0dB_save(j);

% Derivatives
Es = 0.5;
EsN0 = 10^(EsN0dB/10);
N0 =  Es/(EsN0);
    
NErrActual = 0;
NErrCS = 0;
NErrMissed = 0;
NErrFalse = 0;
NErrCRC = 0;

Nsim_save       = zeros(1,numel(EsN0dB_save));
NErrActual_save = zeros(1,numel(EsN0dB_save));
NErrCS_save     = zeros(1,numel(EsN0dB_save));
NErrMissed_save = zeros(1,numel(EsN0dB_save));
NErrFalse_save  = zeros(1,numel(EsN0dB_save));

if exist('results.mat', 'file') == 2
    load('results.mat','EsN0dB_save','Nsim_save','NErrActual_save','NErrCS_save','NErrMissed_save','NErrFalse_save');
end

% CRC-8: Ignore highest 1
gen  = [1, 0, 1, 0, 1, 1, 1, 0, 1]; % x8 + x7 + x4 + x0

for i = 1:Nsim
    % Generate random word
    x = randi([0,1], 1, WordLen * 8);
    % Generate checksums
    crc = calccrc([x, zeros(1,8)],gen);
    x = [x, crc];
    % Generate noise vector 
    w = sqrt(N0/2) .* randn(1, WordLen * 8 + 8);
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

Nsim_save(EsN0dB_save == EsN0dB)        = Nsim_save(EsN0dB_save == EsN0dB) + Nsim;
NErrActual_save(EsN0dB_save == EsN0dB)  = NErrActual_save(EsN0dB_save == EsN0dB) + NErrActual;
NErrCS_save(EsN0dB_save == EsN0dB)      = NErrCS_save(EsN0dB_save == EsN0dB) + NErrCS;
NErrMissed_save(EsN0dB_save == EsN0dB)  = NErrMissed_save(EsN0dB_save == EsN0dB) + NErrMissed;
NErrFalse_save(EsN0dB_save == EsN0dB)   = NErrFalse_save(EsN0dB_save == EsN0dB) + NErrFalse;

Nsim_save(EsN0dB_save == EsN0dB);
NErrMissed_save(EsN0dB_save == EsN0dB);
NErrFalse_save(EsN0dB_save == EsN0dB);
NErrActual_save(EsN0dB_save == EsN0dB);
NErrCS_save(EsN0dB_save == EsN0dB);

simErr = sum((NErrActual_save - NErrCS_save) - NErrMissed_save);

save('results.mat','EsN0dB_save','Nsim_save','NErrActual_save','NErrCS_save','NErrMissed_save','NErrFalse_save')

Nword = Nword + Nsim;

end

figure()
semilogy(EsN0dB_save, NErrActual_save./Nsim_save), hold on
semilogy(EsN0dB_save, NErrCS_save./Nsim_save)
semilogy(EsN0dB_save, NErrMissed_save./Nsim_save)
semilogy(EsN0dB_save, NErrFalse_save./Nsim_save)
xlim([EsN0start,EsN0stop]), grid on, grid minor
legend('Actuall Err','CS det Err', 'Missed Err','False Err')

T = toc
tavgPerWord = T/Nword

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
    f = word(numel(word)-8+1:numel(word));
end

