close all, clear all

t = 1:1000;

%% Sine + noise
% y = 100 * (0.5 + 0.5*sin(2*pi* linspace(0,5,1000)));
% y1 = randi([0,155],1,20);
% t1 = linspace(1,1000,numel(y1));
% y = y + interp1(t1,y1,t);

%% n x Sine

N = 20;
y = zeros(1, numel(t));
for i = 1:N
    y = y + randi([0,511],1,1) * (sin(2*pi* linspace(0,randi([1,15],1,1),1000)));
end

% --> We observe, that the ideal number of interpolation points is equal to
% 2 * the maximum frequency of the signal

%% Log
% y = log2(linspace(1,1e6,1000));

%% Log with sine
% y = log2(linspace(1,1e6,1000)) + sin(2*pi* linspace(0,10,1000));

%% Fixen num of iterations

% tfit = [t(1), t(end)];    
% yfit = interp1(tfit, y(tfit), t);
% 
% figure()
% plot(t,y,'k'), hold on
% plot(t,yfit,'r'), hold off
% 
% for i = 1:20
%     [val, ind] = max(abs(y - yfit));
%     tfit = [tfit(tfit<ind), ind,    tfit(tfit>ind)];
%     yfit = interp1(tfit, y(tfit), t);
%     updplot(t, y, yfit);
%     pause(0.5)
% end

%% Variable num of iterations 
%  Given by num of zero crossings of differentiated signal

ydiff = sign(diff(y));
numzc = sum(ydiff(1:end-1) ~= ydiff(2:end))

tfit = [t(1), t(end)];    
yfit = interp1(tfit, y(tfit), t);

figure()
plot(t,y,'k'), hold on
plot(t,yfit,'r'), hold off

for i = 1:numzc +2
    [val, ind] = max(abs(y - yfit));
    tfit = [tfit(tfit<ind), ind,    tfit(tfit>ind)];
    yfit = interp1(tfit, y(tfit), t);
    updplot(t, y, yfit);
    pause(0.5)
end


function p = updplot(t, y, yfit)
    plot(t,y,'k', 'LineWidth', 1.7), hold on
    plot(t,yfit, 'r', 'LineWidth', 1.5), hold off
    xlim([t(1), t(end)]);
end